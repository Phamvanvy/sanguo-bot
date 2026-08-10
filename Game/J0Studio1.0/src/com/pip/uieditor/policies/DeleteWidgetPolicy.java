package com.pip.uieditor.policies;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

import com.pip.uieditor.commands.DeleteWidgetCommand;

/**
 * 删除Widget的Policy。
 * 因为在选中多个的Widget的时候这个Policy会被调用多次，每次的Host都不同。为了简化，只处理本host相关的Widget的删除，其他的不管。
 * @author Jeffrey
 *
 */
public class DeleteWidgetPolicy extends ComponentEditPolicy {
	
	@Override
	protected Command createDeleteCommand(GroupRequest request) {
		if(!hasParentInList(request.getEditParts())) {
			return new DeleteWidgetCommand((GraphicalEditPart)getHost());
		}
		return null;
	}
	
	/**
	 * 寻找是否有父节点在删除选中之中，如果有就不要生成Command了，只要让父节点删除就可以了
	 * @param l
	 * @return
	 */
	protected boolean hasParentInList(List l) {
		EditPart parent = getHost().getParent();
		while(parent != null) {
			if(l.contains(parent))
				return true;
			parent = parent.getParent();
		}
		return false;
		
	}
	
}
