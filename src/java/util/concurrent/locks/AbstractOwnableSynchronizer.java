package java.util.concurrent.locks;

public abstract class AbstractOwnableSynchronizer implements java.io.Serializable {

    protected AbstractOwnableSynchronizer() { }

    /**
     * state所代表的锁的持有者
     */
    private transient Thread exclusiveOwnerThread;

    /**
     * 设置锁持有者
     */
    protected final void setExclusiveOwnerThread(Thread thread) {
        exclusiveOwnerThread = thread;
    }

    /**
     * 获取锁持有者
     */
    protected final Thread getExclusiveOwnerThread() {
        return exclusiveOwnerThread;
    }
}
