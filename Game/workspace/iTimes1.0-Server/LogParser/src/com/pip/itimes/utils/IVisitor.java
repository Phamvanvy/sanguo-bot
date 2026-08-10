package com.pip.itimes.utils;

import com.pip.itimes.utils.action.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: </p>
 *
 * <p>Company: </p>
 *
 * @author Jeffrey
 * @version 1.0
 */
public interface IVisitor {
    public void visit(GetAttachmentAction action);
    public void visit(IAction action);
    public void visit(SellItemAction action);
    public void visit(SendAttachmentAction action);
    public void visit(BattleResultAction action);
    public void visit(FeedAction action);
    public void visit(GatherAction action);
    public void visit(LoginAction action);
    public void visit(LogoutAction action);
    public void visit(PkBeginAction action);
    public void visit(PkEndAction action);
    public void visit(TaskCompletedAction action);
    public void visit(TeamBattleResultAction action);
    public void visit(ThrowItemAction action);
    public void visit(UseItemAction action);
}
