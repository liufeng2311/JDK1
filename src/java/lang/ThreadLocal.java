package java.lang;

import java.lang.ref.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ThreadLocal<T> {

    /**
     * 每个ThreadLocal实例化时都会确定唯一的hashcode值, 该值经过计算是最能避免hash冲突的
     */
    private final int threadLocalHashCode = nextHashCode();

    private static AtomicInteger nextHashCode = new AtomicInteger();


    private static final int HASH_INCREMENT = 0x61c88647;

    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }

    /**
     * 没有set()值, 调用get()方法时返回的默认值
     */
    protected T initialValue() {
        return null;
    }

    /**
     * 我们重写initialValue()方法时需要一个新类继承ThreadLocal, 官方基于JDK1.8提供了更方便的方式
     */
    public static <S> ThreadLocal<S> withInitial(Supplier<? extends S> supplier) {
        return new SuppliedThreadLocal<>(supplier);
    }

    /**
     * 默认构造函数
     */
    public ThreadLocal() {
    }

    /**
     * 获取值
     */
    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this);
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T) e.value;
                return result;
            }
        }
        return setInitialValue();
    }

    /**
     * 设置初始值,初始值需要放到Map中,否则InheritableThreadLocal中是获取不到该值得
     */
    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }

    /**
     *设置值
     */
    public void set(T value) {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
    }

    /**
     * 删除值
     */
    public void remove() {
        ThreadLocalMap m = getMap(Thread.currentThread());
        if (m != null)
            m.remove(this);
    }

    /**
     * 获取map, 该方法是子线程可以获取复线程数据的关键
     */
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }

    /**
     * 创建Map
     */
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue);
    }

    /**
     * 父线程创建子线程时调用, 将父线程数据复制到子线程中
     */
    static ThreadLocalMap createInheritedMap(ThreadLocalMap parentMap) {
        return new ThreadLocalMap(parentMap);
    }

    /**
     * 父线程创建子线程时, 数据转化预留接口,主线程存在ThreadLocal时, 创建子线程,会报错
     */
    T childValue(T parentValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * JDK1.8创建默认值类
     */
    static final class SuppliedThreadLocal<T> extends ThreadLocal<T> {

        private final Supplier<? extends T> supplier;

        SuppliedThreadLocal(Supplier<? extends T> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        protected T initialValue() {
            return supplier.get();
        }
    }

    /**
     * ThreadLocal中存储数据的真实类
     */
    static class ThreadLocalMap {

        /**
         * ThreadLocal中数据的表现形式, 本质也是一种Map
         * K为 ThreadLocal对象本身, V为我们需要存储的数据
         * K为弱引用, 主要是为了防止外部ThreadLocal对象至空后, 线程池中的线程长期持有该对象, 此时K和V都会造成内存泄漏
         */
        static class Entry extends WeakReference<ThreadLocal<?>> {
            /**
             * The value associated with this ThreadLocal.
             */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
        }

        /**
         * 初始容量16
         */
        private static final int INITIAL_CAPACITY = 16;

        /**
         * 底层Map数组
         */
        private Entry[] table;

        /**
         * 元素数量
         */
        private int size = 0;

        /**
         * 扩容阈值
         */
        private int threshold;

        /**
         * 设置扩容阈值, 具体为2/3
         */
        private void setThreshold(int len) {
            threshold = len * 2 / 3;
        }

        /**
         * 环式获取下一个节点
         */
        private static int nextIndex(int i, int len) {
            return ((i + 1 < len) ? i + 1 : 0);
        }

        /**
         * 环式获取上一个节点
         */
        private static int prevIndex(int i, int len) {
            return ((i - 1 >= 0) ? i - 1 : len - 1);
        }

        /**
         * 实例化Map对象, 懒加载、计算扩容阈值
         */
        ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
            table = new Entry[INITIAL_CAPACITY];
            int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);
            table[i] = new Entry(firstKey, firstValue);
            size = 1;
            setThreshold(INITIAL_CAPACITY);
        }

        /**
         * 实例化对象, 父线程创建子线程时使用的构造函数, 复制父线程已有的数据
         */
        private ThreadLocalMap(ThreadLocalMap parentMap) {
            Entry[] parentTable = parentMap.table;
            int len = parentTable.length;
            setThreshold(len);
            table = new Entry[len];

            for (int j = 0; j < len; j++) {
                Entry e = parentTable[j];
                if (e != null) {
                    @SuppressWarnings("unchecked")
                    ThreadLocal<Object> key = (ThreadLocal<Object>) e.get();
                    if (key != null) {
                        Object value = key.childValue(e.value);
                        Entry c = new Entry(key, value);
                        int h = key.threadLocalHashCode & (len - 1);
                        while (table[h] != null)
                            h = nextIndex(h, len);
                        table[h] = c;
                        size++;
                    }
                }
            }
        }

        /**
         * 从Map中获取指定K对应的V
         */
        private Entry getEntry(ThreadLocal<?> key) {
            int i = key.threadLocalHashCode & (table.length - 1);
            Entry e = table[i];
            //尝试最优解, 假设并未发生hash冲突
            if (e != null && e.get() == key)
                return e;
            else
                return getEntryAfterMiss(key, i, e);
        }

        /**
         * 发生hash冲突后, 寻找冲突后的值
         * 同时完成rehash
         */
        private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
            Entry[] tab = table;
            int len = tab.length;

            while (e != null) {
                ThreadLocal<?> k = e.get();
                if (k == key)
                    return e;
                if (k == null) //说明出现了无效数据, 需要清理
                    expungeStaleEntry(i);
                else
                    i = nextIndex(i, len);
                e = tab[i];
            }
            return null;
        }

        /**
         * 赋值
         */
        private void set(ThreadLocal<?> key, Object value) {

            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len - 1);

            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                ThreadLocal<?> k = e.get();
                //找到对应的K, 替换值
                if (k == key) {
                    e.value = value;
                    return;
                }
                //找到过期的数据
                if (k == null) {
                    replaceStaleEntry(key, value, i);
                    return;
                }
            }

            //找到一个空槽,将数据放在空槽里
            tab[i] = new Entry(key, value);
            int sz = ++size;
            //判断是否需要进行rehash
            if (!cleanSomeSlots(i, sz) && sz >= threshold)
                rehash();
        }

        /**
         * 删除数据
         */
        private void remove(ThreadLocal<?> key) {
            Entry[] tab = table;
            int len = tab.length;
            int i = key.threadLocalHashCode & (len - 1);
            for (Entry e = tab[i]; e != null; e = tab[i = nextIndex(i, len)]) {
                if (e.get() == key) {
                    e.clear();
                    //删除元素后,此时的数据还未从Map中删除, 需要触发清理
                    expungeStaleEntry(i);
                    return;
                }
            }
        }

        /**
         * 赋值时, 发现无效数据的处理
         * 次数我们的K可能在无效数据后面, 也可能不存在，需要分情况处理
         */
        private void replaceStaleEntry(ThreadLocal<?> key, Object value, int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            Entry e;
            int slotToExpunge = staleSlot;
            //找到最早的无效数据所在的槽位
            for (int i = prevIndex(staleSlot, len); (e = tab[i]) != null; i = prevIndex(i, len))
                if (e.get() == null) {
                    slotToExpunge = i;
                }

            // Find either the key or trailing null slot of run, whichever
            // occurs first
            for (int i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();

                if (k == key) {
                    // 替换K到无效数据处
                    e.value = value;
                    tab[i] = tab[staleSlot];
                    tab[staleSlot] = e;

                    // Start expunge at preceding stale entry if it exists
                    if (slotToExpunge == staleSlot)
                        slotToExpunge = i;
                    cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
                    return;
                }

                // 通过向前查找没有找到过期数据, 但是向后查找找到了过期数据, 我们记录第一个过期数据的节点即可
                //slotToExpunge == staleSlot表示向前查找没有失效数据,k == null表示向后查找存在失效数据, 此时slotToExpunge值发生改变, 导致slotToExpunge == staleSlot不再成立
                //该分支
                if (k == null && slotToExpunge == staleSlot)
                    slotToExpunge = i;
            }

            // 将新增的数据放置到固定槽位
            tab[staleSlot].value = null;
            tab[staleSlot] = new Entry(key, value);

            // 如果存在其他无效数据, 触发清理
            if (slotToExpunge != staleSlot)
                cleanSomeSlots(expungeStaleEntry(slotToExpunge), len);
        }

        /**
         * 删除指定槽位的无效数据, 并对后面的数据进行校验(重hash), 返回第一个不存在数据的索引
         */
        private int expungeStaleEntry(int staleSlot) {
            Entry[] tab = table;
            int len = tab.length;
            tab[staleSlot].value = null;
            tab[staleSlot] = null;
            size--;
            Entry e;
            int i;
            for (i = nextIndex(staleSlot, len); (e = tab[i]) != null; i = nextIndex(i, len)) {
                ThreadLocal<?> k = e.get();
                if (k == null) {
                    e.value = null;
                    tab[i] = null;
                    size--;
                } else {
                    int h = k.threadLocalHashCode & (len - 1);
                    if (h != i) {
                        tab[i] = null;
                        while (tab[h] != null)
                            h = nextIndex(h, len);
                        tab[h] = e;
                    }
                }
            }
            return i;
        }

        /**
         * 处理n >>>= 1 清除工作,
         */
        private boolean cleanSomeSlots(int i, int n) {
            boolean removed = false;
            Entry[] tab = table;
            int len = tab.length;
            do {
                i = nextIndex(i, len);
                Entry e = tab[i];
                //当遇到空槽时, 后续的循环不执行任何逻辑, 最差的情况n >>>= 1次空循环
                if (e != null && e.get() == null) {
                    n = len;
                    removed = true;
                    i = expungeStaleEntry(i);
                }
            } while ((n >>>= 1) != 0);
            return removed;
        }

        /**
         * 扩容操作
         */
        private void rehash() {
            //扩容前, 需要确保数组中不存在无效数据
            expungeStaleEntries();

            // 达扩容条件, 触发扩容
            if (size >= threshold - threshold / 4)
                resize();
        }

        /**
         * 双倍扩容
         */
        private void resize() {
            Entry[] oldTab = table;
            int oldLen = oldTab.length;
            int newLen = oldLen * 2;
            Entry[] newTab = new Entry[newLen];
            int count = 0;

            for (int j = 0; j < oldLen; ++j) {
                Entry e = oldTab[j];
                if (e != null) {
                    ThreadLocal<?> k = e.get();
                    if (k == null) {
                        e.value = null; // Help the GC
                    } else {
                        int h = k.threadLocalHashCode & (newLen - 1);
                        while (newTab[h] != null)
                            h = nextIndex(h, newLen);
                        newTab[h] = e;
                        count++;
                    }
                }
            }

            setThreshold(newLen);
            size = count;
            table = newTab;
        }

        /**
         * 清除所有的无效数据
         */
        private void expungeStaleEntries() {
            Entry[] tab = table;
            int len = tab.length;
            for (int j = 0; j < len; j++) {
                Entry e = tab[j];
                if (e != null && e.get() == null)
                    expungeStaleEntry(j);
            }
        }
    }
}

/**
 * scplus.autohome.com.cn
 * activity-recommend.corpautohome.com
 * activity-recommend.autohome.com.cn
 * ahohcrm.autohome.com.cn
 * ahohcrm.api.corpautohome.com
 * sales.autohome.com.cn
 * page.autohome.com.cn
 * page.in.corpautohome.com
 * vtd.autohome.com.cn
 * ahohcrm-stresstest.corpautohome.com
 * activity.admin.corpautohome.com
 * adproxy.autohome.com.cn
 * pcmx.autohome.com.cn
 */
