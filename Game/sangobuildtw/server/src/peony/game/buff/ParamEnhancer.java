package peony.game.buff;

/**
 * 技能或BUFF参数增强接口。某些BUFF会影响技能或其他BUFF的效果，它们就需要实现这样一个接口。
 * @author lighthu
 */
public interface ParamEnhancer {
    void getEnhanceParams(ParamEnhanceSet enhanceSet);
    void removeEnhanceParams(ParamEnhanceSet enhanceSet);
}
