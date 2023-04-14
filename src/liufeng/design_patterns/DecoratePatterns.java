package liufeng.design_patterns;

/**
 * 装饰模式
 * 对已存在的功能进行扩展, 扩展类需要实现已有功能接口,抽象扩展类限制扩展类必须引用已存在的类
 * @author liufeng
 * @date 2021/10/29
 */


/**
 * 功能接口
 */
public interface DecoratePatterns {

    void operation();
}


/**
 * 已存在功能
 *
 */
class Component implements DecoratePatterns {

    @Override
    public void operation() {
        System.out.println("我是已存在功能");
    }
}


/**
 * 抽象类扩展类,该类存在的意义是限定扩展类必须通过构造函数将已存在的类传进去
 * 且调用已有类的功能
 */
abstract class AbstractExtendComponent implements DecoratePatterns {

    DecoratePatterns decoratePatterns;

    public AbstractExtendComponent(DecoratePatterns decoratePatterns) {
        this.decoratePatterns = decoratePatterns;
    }

    @Override
    public void operation() {
        decoratePatterns.operation();
    }
}

/**
 * 具体的扩展类,调用已存在的类并扩展自己的方法
 */
class ExtendComponent extends AbstractExtendComponent {

    public ExtendComponent(DecoratePatterns decoratePatterns) {
        super(decoratePatterns);
    }

    @Override
    public void operation() {
        super.operation();
        additionalFunction();
    }


    private void additionalFunction() {
        System.out.println("我是额外的扩展功能");

    }
}