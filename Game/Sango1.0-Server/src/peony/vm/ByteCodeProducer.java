package peony.vm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import peony.game.ExpressionNode;
import peony.game.GameQuest;
import peony.game.StringExpression;
import peony.game.StringExpressionNode;
import peony.game.StringNode;
import peony.script.BinaryExpression;
import peony.script.Constant;
import peony.script.Expression;
import peony.script.ExpressionList;
import peony.script.Function;
import peony.script.Operator;
import peony.script.Trigger;
import peony.script.TripleExpression;
import peony.script.Value;
import peony.script.Variable;

public class ByteCodeProducer implements Opcodes {

	// private static final Logger log =
	// Logger.getLogger(ByteCodeProducer.class);

	protected GameQuest quest;
	protected ClassWriter cw;
	protected CheckClassAdapter cv;
	protected TraceClassVisitor tv;

	protected static final String INT_CONST_PREFIX = "INTCONST";
	protected static final String STRING_CONST_PREFIX = "STRINGCONST";

	protected int intconstcount = 0; // 已经生成的int类型的常量个数
	protected int stringconstcount = 0; // 已经生成的string类型的常量的个数
	protected Map<String, String> stringconst2name = new HashMap<String, String>(); // string类型常量值到生成的常量名的对照表
	protected Map<Integer, String> intconst2name = new HashMap<Integer, String>(); // int类型的常量值到生成的常量名的对照表
	protected String internalClassName;
	protected String className;

	protected static final String[][] methodTables = {
			{ "Random", "random", "()I" },
			{ "AssignTask", "assignTask", "(I)I" },
			{ "EndTask", "endTask", "(I)I" },
			{ "HasTask", "hasTask", "(I)I" },
			{ "TaskFinished", "taskFinished", "(I)I" },
			{ "GetReward", "getReward", "(I)I" },
			{ "CanFinish", "canFinish", "()I" },
			{ "SetFail", "setFail", "()I" },
			{ "Chat", "chat", "(ILjava/lang/String;I)I" },
			{ "Message", "message", "(Ljava/lang/String;II)I" },
			{ "Question", "question",
					"(Ljava/lang/String;Ljava/lang/String;I)I" },
			{ "RefreshNPC", "refreshNPC", "(III)I" },
			{ "RefreshNPCAt", "refreshNPCAt", "(III)I" },
			{ "GotoMap", "gotoMap", "(III)I" }, { "Logout", "logout", "()I" },
			{ "FindNPCByType", "findNPCByType", "(III)I" },
			{ "FindNPC", "findNPC", "(II)I" },
			{ "FindPlayer", "findPlayer", "(II)I" },
			{ "HasItem", "hasItem", "(II)I" },
			{ "AddItem", "addItem", "(II)I" },
			{ "AddBuff", "addBuff", "(II)I" },
			{ "RemoveItem", "removeItem", "(II)I" },
			{ "MoveNPC", "moveNPC", "(IIII)I" },
			{ "RemoveNPC", "removeNPC", "(I)I" },
			{ "RemoveNPCByID", "removeNPCById", "(I)I" },
			{ "OpenUI", "openui", "(Ljava/lang/String;)I" },
			{ "SavePosition", "savePosition", "(Ljava/lang/String;)I" },
			{ "DecActivePower", "decActivePower", "(I)I" },
			{ "FindNPCAt", "findNPCAt", "(IIIII)I" },
			{ "HasTitle", "hasTitle", "(I)I" },
			{ "NpcShout", "shout", "(ILjava/lang/String;I)I"},
			{ "E_Approach", "e_Approach", "(III)I" },
			{ "E_FirstApproach", "e_FirstApproach", "(III)I" },
			{ "E_EnterMap", "e_EnterMap", "(I)I" },
			{ "E_Kill", "e_Kill", "(III)I" },
			{ "E_KillWithMate", "e_KillWithMate", "(III)I" },
			{ "E_KillPlayer", "e_KillPlayer", "(I)I" },
			{ "E_UseSkill", "e_UseSkill", "(I)I" },
			{ "E_TouchNPC", "e_TouchNPC", "(I)I" },
			{ "E_Killed", "e_Killed", "()I" },
			{ "E_KilledByPlayer", "e_KilledByPlayer", "()I" },
			{ "E_UseItem", "e_UseItem", "(I)I" },
			{ "E_Chat", "e_Chat", "()I" }, 
			{ "E_OpenUI", "e_OpenUI", "(I)I" },
			{ "E_AnswerQuestion", "e_AnswerQuestion", "(II)I" },
			{ "E_CloseChat", "e_CloseChat", "(I)I" },
			{ "E_CloseMessage", "e_CloseMessage", "(I)I" },
			{ "E_TaskFinish", "e_TaskFinish", "()I" },
			{ "E_HasAction", "e_HasAction", "(I)I"},
			{ "IsChannel","isChannel","(Ljava/lang/String;)I"},
			{ "E_IsInTongBattle","e_IsInTongBattle","()I"},
			{ "E_IsInNationBattle","e_IsInNationBattle","()I"},
			{ "E_IsInFlagBattle","e_IsInFlagBattle","()I"},
			{ "E_KillPlayers","e_KillPlayers","()I"},
			{ "E_ApprochPosition", "e_ApprochPosition", "(IIII)I"},
			{ "IsCity","isCity","(Ljava/lang/String;)I"},
			{ "InjoinAssociation", "injoinAssociation", "()I" },
			{ "E_KillWithAssociation", "e_killWithAssociation", "(III)I" },
			{ "Bubble", "bubble", "(ILjava/lang/String;I)I" },
			{ "Shout", "lionshout", "(Ljava/lang/String;)I"},
			{ "E_KillWithTeacher", "e_killWithTeacher", "(III)I" },
			{ "E_KillPlayersLimited", "e_killPlayersLimited", "(III)I" },
			{ "E_ReachPearls","e_reachPearls","(III)I"},
			{ "HasBuff", "hasBuff", "(I)I" },
	};

	protected static final Map<String, String> METHODS = new HashMap<String, String>();
	static {
		for (int i = 0; i < methodTables.length; i++) {
			METHODS.put(methodTables[i][0].toUpperCase(), methodTables[i][1]);
		}
	}

	protected static final Map<String, String> SIGNS = new HashMap<String, String>();
	static {
		for (int i = 0; i < methodTables.length; i++) {
			SIGNS.put(methodTables[i][0].toUpperCase(), methodTables[i][2]);
		}
	}

	public ByteCodeProducer(GameQuest quest) {
		this.quest = quest;
	}
	
	public byte[] produce() {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
//		tv = new TraceClassVisitor(cw,new PrintWriter(System.out));
		cv = new CheckClassAdapter(cw);
		internalClassName = "L"+quest.getClassName().replace('.', '/');
		className = internalClassName.substring(1);
		// log.debug(internalClassName);
		cv.visit(V1_5, ACC_PUBLIC + ACC_SUPER, internalClassName.substring(1),
				null, "peony/vm/AbstractASMQuest", null);
		// log.debug(String.format("class:%s",internalClassName.substring(1)));
		visitConsts();
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>",
				"(Lpeony/game/GameQuest;)V", null, null);
		// log.debug("Method <init>");
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		// log.debug("ALOAD 0");
		mv.visitVarInsn(ALOAD, 1);
		// log.debug("ALOAD 1");
		mv.visitMethodInsn(INVOKESPECIAL, "peony/vm/AbstractASMQuest",
				"<init>", "(Lpeony/game/GameQuest;)V");
		// log.debug("INVOKESPECIAL Super<init>");
		// log.debug("RETURN");
		mv.visitInsn(RETURN);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		visitPreCondition();
		visitTriggers();
		visitFinishCondition();
		visitDesc("getDesc", quest.getDesc());
		visitDesc("getPreDesc", quest.getPreDesc());
		visitDesc("getPostDesc", quest.getPostDesc());
		visitDesc("getUnFinishDesc", quest.getUnFinishDesc());
//		tv.visitEnd();
		cw.visitEnd();
		return cw.toByteArray();
	}

	protected void visitDesc(String name, StringExpression desc) {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, name,
				"(Lpeony/vm/ASMGameVM;)Ljava/lang/String;", null, null);
		List<StringExpressionNode> nodes = desc.getNodes();
		if (nodes.size() == 1 && nodes.get(0) instanceof StringNode) {
			mv.visitCode();
			mv.visitLdcInsn(((StringNode) nodes.get(0)).value);
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		} else {
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 1);
			mv.visitFieldInsn(GETFIELD, "peony/vm/ASMGameVM", "player",
					"Lpeony/game/Player;");
			mv.visitVarInsn(ASTORE, 2);
			mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder",
					"<init>", "()V");
			mv.visitVarInsn(ASTORE, 3);
			for (StringExpressionNode node : nodes) {
				if (node instanceof StringNode) {
					mv.visitVarInsn(ALOAD, 3);
					mv.visitLdcInsn(((StringNode) node).value);
					mv.visitMethodInsn(INVOKEVIRTUAL,
							"java/lang/StringBuilder", "append",
							"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
					mv.visitInsn(POP);
				} else if (node instanceof ExpressionNode) {
					mv.visitVarInsn(ALOAD, 3);
					Expression exp = ((ExpressionNode) node).value;
					visitExpressionInFunction(mv, exp);
					if (exp.getReturnType() == Value.STRING)
						mv
								.visitMethodInsn(INVOKEVIRTUAL,
										"java/lang/StringBuilder", "append",
										"(Ljava/lang/String;)Ljava/lang/StringBuilder;");
					else if (exp.getReturnType() == Value.INT)
						mv.visitMethodInsn(INVOKEVIRTUAL,
								"java/lang/StringBuilder", "append",
								"(I)Ljava/lang/StringBuilder;");
					else
						throw new IllegalArgumentException();
					mv.visitInsn(POP);
				}

			}
			mv.visitVarInsn(ALOAD, 3);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder",
					"toString", "()Ljava/lang/String;");
			mv.visitInsn(ARETURN);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
	}

	protected void visitPreCondition() {
		ExpressionList el = quest.getPreCondition();
		visitConditionMethod("preCondition", el);
	}

	/**
	 * 处理前置后置条件,如果没有定义就直接
	 * 
	 * @param name
	 * @param el
	 */
	protected void visitConditionMethod(String name, ExpressionList el) {
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, name,
				"(Lpeony/vm/ASMGameVM;)I", null, null);
		// log.debug(String.format("Method %s", name));
		if (el == null || el.getExpressions().length == 0) {
			mv.visitCode();
			mv.visitInsn(ICONST_1);
			// log.debug("ICONST_0");
			mv.visitInsn(IRETURN);
			// log.debug("IRETURN");
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		} else {
			mv.visitCode();
			Label l0 = new Label();
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			mv.visitFieldInsn(GETFIELD, "peony/vm/ASMGameVM", "player",
					"Lpeony/game/Player;");
			// log.debug(String.format("GETFIELD %s", "player"));
			mv.visitVarInsn(ASTORE, 2);
			// log.debug("ASTORE 2");
			visitCondition(mv, el, l0);
			mv.visitInsn(ICONST_1);
			// log.debug("ICONST_1");
			mv.visitInsn(IRETURN);
			// log.debug("IRETURN");
			mv.visitLabel(l0);
			// log.debug("LABEL "+l0);
			mv.visitInsn(ICONST_0);
			// log.debug("ICONST_0");
			mv.visitInsn(IRETURN);
			// log.debug("IRETURN");
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
	}

	protected void visitCondition(MethodVisitor mv, ExpressionList el, Label l0) {
		Expression[] exps = el.getExpressions();
		for (int i = 0; i < exps.length; i++) {
			if (exps[i] instanceof Constant) {
				visitConst(mv, (Constant) exps[i], l0);
			} else if (exps[i] instanceof Variable) {
				visitVariable(mv, (Variable) exps[i], l0);
			} else if (exps[i] instanceof BinaryExpression) {
				visitBinaryExpression(mv, (BinaryExpression) exps[i], l0);
			} else if (exps[i] instanceof TripleExpression) {
				visitTripleExpression(mv, (TripleExpression) exps[i], l0);
			} else if (exps[i] instanceof Function) {
				visitFunction(mv, (Function) exps[i], l0);
			}
		}
	}

	protected void visitAction(MethodVisitor mv, ExpressionList el) {
		Expression[] exps = el.getExpressions();
		for (int i = 0; i < exps.length; i++) {
			if (exps[i] instanceof Constant) {
				visitConst(mv, (Constant) exps[i], null);
				mv.visitInsn(POP);
				// log.debug("POP");
			} else if (exps[i] instanceof Variable) {
				visitVariable(mv, (Variable) exps[i], null);
				mv.visitInsn(POP);
				// log.debug("POP");
			} else if (exps[i] instanceof BinaryExpression) {
				visitBinaryExpression(mv, (BinaryExpression) exps[i], null);
				mv.visitInsn(POP);
				// log.debug("POP");
			} else if (exps[i] instanceof TripleExpression) {
				visitTripleExpression(mv, (TripleExpression) exps[i], null);
				mv.visitInsn(POP);
				// log.debug("POP");
			} else if (exps[i] instanceof Function) {
				visitFunction(mv, (Function) exps[i], null);
				mv.visitInsn(POP);
				// log.debug("POP");
			}
		}
	}

	protected void visitTripleExpression(MethodVisitor mv,
			TripleExpression exp, Label l) {
		Expression[] exps = exp.getExpressions();
		visitExpressionInFunction(mv, exps[0]);
		Label l1 = new Label();
		mv.visitJumpInsn(IFEQ, l1);
		// log.debug("IFEQ");
		visitExpressionInFunction(mv, exps[1]);
		Label l3 = new Label();
		mv.visitJumpInsn(GOTO, l3);
		// log.debug("GOTO "+l3);
		mv.visitLabel(l1);
		// log.debug("LABEL "+l1);
		visitExpressionInFunction(mv, exps[2]);
		mv.visitLabel(l3);
		// log.debug("LABEL"+l3);
		if (l != null) {
			mv.visitJumpInsn(IFEQ, l);
			// log.debug("IFEQ "+l);
		}
	}

	protected void visitBinaryExpression(MethodVisitor mv,
			BinaryExpression exp, Label l) {
		Expression[] exps = exp.getExpressions();
		for (int i = 0; i < exps.length; i++) {
			if (exps[i] instanceof Constant) {
				visitConst(mv, (Constant) exps[i], null);
			} else if (exps[i] instanceof Variable) {
				visitVariable(mv, (Variable) exps[i], null);

			} else if (exps[i] instanceof Function) {
				visitFunction(mv, (Function) exps[i], null);
			} else
				throw new IllegalArgumentException();
		}
		int op = exp.getOperator();
		int opcode = 0;
		if (op == Operator.EQ)
			opcode = IF_ICMPNE;
		else if (op == Operator.GE)
			opcode = IF_ICMPLT;
		else if (op == Operator.GT)
			opcode = IF_ICMPLE;
		else if (op == Operator.LE)
			opcode = IF_ICMPGT;
		else if (op == Operator.LT)
			opcode = IF_ICMPGE;
		else if (op == Operator.NE)
			opcode = IF_ICMPEQ;
		else
			throw new IllegalArgumentException();
		mv.visitJumpInsn(opcode, l);
		// log.debug("Jump "+l);
	}

	protected int getFieldIndex(Constant exp) {
		String fieldName = exp.getValue(null).stringValue();
		return Integer.parseInt(fieldName.substring(1)); // 变量都是以V开头加数字，数字就是变量的index位置
		// return quest.getScript().getFieldIndex(fieldName);
	}

	/**
	 * 处理在函数调用里面的表达式
	 * 
	 * @param mv
	 * @param exp
	 */
	protected void visitExpressionInFunction(MethodVisitor mv, Expression exp) {
		if (exp instanceof Variable) {
			visitVariable(mv, (Variable) exp, null);
		} else if (exp instanceof Constant) {
			visitConst(mv, (Constant) exp, null);
		} else if (exp instanceof Function) {
			visitFunction(mv, (Function) exp, null);
		} else
			throw new IllegalArgumentException();
	}

	protected void visitFunction(MethodVisitor mv, Function exp, Label l) {
		String function = exp.getName();
		Expression[] exps = exp.getExpressions();
		if ("Set".equalsIgnoreCase(function)) {
			String fieldName = ((Constant) exps[0]).getValue(null)
					.stringValue();
			if (fieldName.startsWith("__")) {
				mv.visitVarInsn(ALOAD, 1);
				mv.visitLdcInsn(fieldName);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"setGlobalValue", "(Ljava/lang/String;I)I");
			} else {
				int index = getFieldIndex((Constant) exps[0]);
				mv.visitVarInsn(ALOAD, 1);
				// log.debug("ALOAD 1");
				mv.visitVarInsn(ALOAD, 0);
				// log.debug("ALOAD 0");
				mv.visitFieldInsn(GETFIELD, className, "id", "I");
				// log.debug(String.format("GETFIELD %s id", className));
				pushIntConst(mv, index);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"setValue", "(III)I");
				// log.debug("INVOKE setValue");
			}
		} else if ("Inc".equalsIgnoreCase(function)) {
			String fieldName = ((Constant) exps[0]).getValue(null)
					.stringValue();
			if (fieldName.startsWith("__")) {
				mv.visitVarInsn(ALOAD,1);
				mv.visitLdcInsn(fieldName);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"changeGlobalValue", "(Ljava/lang/String;I)I");
			} else {
				int index = getFieldIndex((Constant) exps[0]);
				mv.visitVarInsn(ALOAD, 1);
				// log.debug("ALOAD 1");
				mv.visitVarInsn(ALOAD, 0);
				// log.debug("ALOAD 0");
				mv.visitFieldInsn(GETFIELD, className, "id", "I");
				// log.debug(String.format("GETFIELD %s id", className));
				pushIntConst(mv, index);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"addValue", "(III)I");
			}
			// log.debug("INVOKE addValue");
		} else if ("Dec".equalsIgnoreCase(function)) {
			String fieldName = ((Constant) exps[0]).getValue(null)
					.stringValue();
			if (fieldName.startsWith("__")) {
				mv.visitVarInsn(ALOAD,1);
				mv.visitLdcInsn(fieldName);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"changeGlobalValue", "(Ljava/lang/String;I)I");
			} else {
				int index = getFieldIndex((Constant) exps[0]);
				mv.visitVarInsn(ALOAD, 1);
				// log.debug("ALOAD 1");
				mv.visitVarInsn(ALOAD, 0);
				// log.debug("ALOAD 0");
				mv.visitFieldInsn(GETFIELD, className, "id", "I");
				// log.debug(String.format("GETFIELD %s id", className));
				pushIntConst(mv, index);
				visitExpressionInFunction(mv, exps[1]);
				mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
						"decValue", "(III)I");
			}
			// log.debug("INVOKE decValue");
		} else if ("E_Kill".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[1]);
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			visitExpressionInFunction(mv, exps[0]);
			pushIntConst(mv, index);
			visitExpressionInFunction(mv, exps[2]);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "e_Kill",
					"(III)I");
			// log.debug("INVOKE e_Kill");
		} else if ("E_KillWithMate".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[1]);
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			visitExpressionInFunction(mv, exps[0]);
			pushIntConst(mv, index);
			visitExpressionInFunction(mv, exps[2]);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "e_KillWithMate",
					"(III)I");
			// log.debug("INVOKE e_KillWithMate");
		}else if ("E_KillWithAssociation".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[1]);
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			visitExpressionInFunction(mv, exps[0]);
			pushIntConst(mv, index);
			visitExpressionInFunction(mv, exps[2]);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "e_killWithAssociation",
					"(III)I");
			// log.debug("INVOKE e_KillWithMate");
		}else if("E_KillWithTeacher".equalsIgnoreCase(function)){
			int index = getFieldIndex((Constant) exps[1]);
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			visitExpressionInFunction(mv, exps[0]);
			pushIntConst(mv, index);
			visitExpressionInFunction(mv, exps[2]);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "e_killWithTeacher",
					"(III)I");
	    }else if ("RefreshNPC".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[2]);
			mv.visitVarInsn(ALOAD, 1);
			visitExpressionInFunction(mv, exps[0]);
			visitExpressionInFunction(mv, exps[1]);
			pushIntConst(mv, index);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
					"refreshNPC", "(III)I");
		} else if ("RefreshNPCAt".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[2]);
			mv.visitVarInsn(ALOAD, 1);
			visitExpressionInFunction(mv, exps[0]);
			visitExpressionInFunction(mv, exps[1]);
			pushIntConst(mv, index);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
					"refreshNPCAt", "(III)I");
		} else if ("FindNPCByType".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[2]);
			mv.visitVarInsn(ALOAD, 1);
			visitExpressionInFunction(mv, exps[0]);
			visitExpressionInFunction(mv, exps[1]);
			pushIntConst(mv, index);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
					"findNPCByType", "(III)I");
		}else if ("E_ReachPearls".equalsIgnoreCase(function)) {
			int index = getFieldIndex((Constant) exps[1]);
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			visitExpressionInFunction(mv, exps[0]);
			pushIntConst(mv, index);
			visitExpressionInFunction(mv, exps[2]);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "e_reachPearls",
					"(III)I");
        }else {
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			for (int i = 0; i < exps.length; i++) {
				visitExpressionInFunction(mv, exps[i]);
			}
			String s = METHODS.get(function.toUpperCase());
			String sign = SIGNS.get(function.toUpperCase());
			if (s == null)
				throw new IllegalArgumentException(String.format(
						"%s method not found", function));
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", s, sign);
			// log.debug(String.format("INVOKE %s", s));
		}
		if (l != null) {
			mv.visitJumpInsn(IFEQ, l);
			// log.debug("IFEQ "+l);
		}
	}

	protected void visitVariable(MethodVisitor mv, Variable exp, Label l) {
		String name = exp.getName();
		if (name.startsWith("_"))
			visitSystemVariable(mv, exp, l);
		else {
			int index = quest.getScript().getFieldIndexFromV(exp.getName());
			mv.visitVarInsn(ALOAD, 1);
			// log.debug("ALOAD 1");
			mv.visitVarInsn(ALOAD, 0);
			// log.debug("ALOAD 0");
			mv.visitFieldInsn(GETFIELD, className, "id", "I");
			// log.debug(String.format("GETFIELD %s id", className));
			pushIntConst(mv, index);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM", "getValue",
					"(II)I");
			// log.debug("INVOKE getValue");
			if (l != null) {
				mv.visitJumpInsn(IFEQ, l);
				// log.debug("IFEQ "+l);
			}
		}
	}

	/**
	 * 系统变量大部分是直接取值，有部分需要进行函数调用
	 */
	protected void visitSystemVariable(MethodVisitor mv, Variable exp, Label l) {
		String s = exp.getName().toUpperCase();
		// mv.visitVarInsn(ALOAD, 0);
		if ("_LEVEL".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "level", "I");
			// log.debug("GETFIELD level");
		} else if ("_MONEY".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getMoney",
					"()I");
			// log.debug("GETFIELD money");
		} else if ("_IMONEY".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getIMoney",
					"()I");
			// log.debug("GETFIELD money");
		} else if ("_HP".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "hp", "I");
			// log.debug("GETFIELD hp");
		} else if ("_MAXHP".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "maxhp", "I");
			// log.debug("GETFIELD maxhp");
		} else if ("_MP".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "mp", "I");
			// log.debug("GETFIELD mp");
		} else if ("_MAXMP".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "maxmp", "I");
			// log.debug("GETFIELD maxmp");
		} else if ("_STR".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "strength", "I");
			// log.debug("GETFIELD strength");
		} else if ("_STA".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "stamina", "I");
			// log.debug("GETFIELD vitality");
		} else if ("_INT".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "intellect",
					"I");
			// log.debug("GETFIELD intelligence");
		} else if ("_AGI".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "agility", "I");
			// log.debug("GETFIELD agility");
		} else if ("_SEX".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "sex", "I");
			// log.debug("GETFIELD sex");
		} else if ("_SEXNAME".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player",
					"getSexName", "()Ljava/lang/String;");
			// log.debug("INVOKE getSexName");
		} else if ("_CLASS".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "clazz", "I");
			// log.debug("GETFIELD clazz");
		} else if ("_CLASSNAME".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player",
					"getClassName", "()Ljava/lang/String;");
			// log.debug("INVOKE getClassName");
        } else if ("_FACTION".equals(s)) {
            mv.visitVarInsn(ALOAD, 2);
            // log.debug("ALOAD 2");
            mv.visitFieldInsn(GETFIELD, "peony/game/Player", "faction", "I");
            // log.debug("GETFIELD faction");
        } else if ("_FACTIONNAME".equals(s)) {
            mv.visitVarInsn(ALOAD, 2);
            // log.debug("ALOAD 2");
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player",
                    "getFactionName", "()Ljava/lang/String;");
            // log.debug("INVOKE getFactionName");
		} else if ("_NAME".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "name",
					"Ljava/lang/String;");
			// log.debug("GETFIELD name");
		} else if ("_MAPID".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "map",
					"Lpeony/game/VMap;");
			// log.debug("GETFIELD map");
			mv
					.visitMethodInsn(INVOKEVIRTUAL, "peony/game/VMap", "getId",
							"()I");
			// log.debug("INVOKE getId");
		} else if ("_X".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "x", "I");
			// log.debug("GETFIELD x");
		} else if ("_Y".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			// log.debug("ALOAD 2");
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "y", "I");
			// log.debug("GETFIELD y");
		} else if ("_KEYBOARDTYPE".equals(s)) {
		    mv.visitVarInsn(ALOAD, 2);
            // log.debug("ALOAD 2");
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getKeyboardType", "()I");
            // log.debug("GETFIELD keyboardType");
        } else if ("_MOUSETYPE".equals(s)) {
            mv.visitVarInsn(ALOAD, 2);
            // log.debug("ALOAD 2");
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getMouseType", "()I");
            // log.debug("GETFIELD mouseType");
        } else if ("_MATEID".equals(s)) {
            mv.visitVarInsn(ALOAD, 2);
            // log.debug("ALOAD 2");
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getMateID", "()I");
            // log.debug("GETFIELD mateID");
		} else if (s.startsWith("__")){
			mv.visitVarInsn(ALOAD,1);
			mv.visitLdcInsn(exp.getName());
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/vm/ASMGameVM",
					"getGlobalValue", "(Ljava/lang/String;)I");
		} else if ("_ISKING".equals(s)){
			mv.visitVarInsn(ALOAD, 2);
			 mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "isKing", "()I");
		} else if ("_ISOFFICER".equals(s)){
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "isOfficer", "()I");
		} else if ("_HASTONG".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "hasGuild", "()I");
		} else if ("_ACTIVEPOWER".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			mv.visitFieldInsn(GETFIELD, "peony/game/Player", "activePower", "I");
		} else if ("_ADDEDPROPERTYPOINT".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getAddedPropertyPoint", "()I");
		} else if ("_ADDEDSKILLPOINT".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "getAddedSkillPoint", "()I");
		} else if ("_RECEIVEASSOINV".equals(s)) {
			mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "hasReceiveAssoInv", "()I");
		} else if ("_ASSOCIATIONINVITE".equals(s)){
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "hasReceiveAssoInv", "()I");
		} else if("_HAVEASSOCIATION".equals(s)){
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "hasAssociation", "()I");
		} else if ("_FOLLOWATTENDANT".equals(s)){
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "peony/game/Player", "attendantIsFollowing", "()I");
       }
		if (l != null) {
			mv.visitJumpInsn(IFEQ, l);
			// log.debug("IFEQ "+l);
		}
	}

	protected void pushIntConst(MethodVisitor mv, int n) {
		if (n == -1) {
			mv.visitInsn(ICONST_M1);
			// log.debug("ICONST_M1");
		} else if (n == 0) {
			mv.visitInsn(ICONST_0);
			// log.debug("ICONST_0");
		} else if (n == 1) {
			mv.visitInsn(ICONST_1);
			// log.debug("ICONST_1");
		} else if (n == 2) {
			mv.visitInsn(ICONST_2);
			// log.debug("ICONST_2");
		} else if (n == 3) {
			mv.visitInsn(ICONST_3);
			// log.debug("ICONST_3");
		} else if (n == 4) {
			mv.visitInsn(ICONST_4);
			// log.debug("ICONST_4");
		} else if (n == 5) {
			mv.visitInsn(ICONST_5);
			// log.debug("ICONST_5");
		} else if (n < 128 && n > -129) {
			mv.visitIntInsn(BIPUSH, n);
			// log.debug(String.format("BIPUSH %d",n));
		} else {
			mv.visitIntInsn(SIPUSH, n);
			// log.debug(String.format("SIPUSH %d", n));
		}
	}

	protected void visitConst(MethodVisitor mv, Constant exp, Label l) {
		if (l != null && exp.getValue(null).getType() == Value.INT) {
			if (exp.getValue(null).intValue() != 0)
				return;
			else {
				mv.visitJumpInsn(GOTO, l);
				// log.debug("GOTO");
			}
		} else {
			mv.visitLdcInsn(exp.getValue(null).get());
			// log.debug(String.format("Ldc %s", exp.getValue(null).get()));
		}

	}

	protected void visitTriggers() {
		Trigger[] triggers = quest.getScript().getTriggers();
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "execute",
				"(Lpeony/vm/ASMGameVM;)V", null, null);
		// log.debug("METHOD execute");
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 1);
		// log.debug("ALOAD 1");
		mv.visitFieldInsn(GETFIELD, "peony/vm/ASMGameVM", "player",
				"Lpeony/game/Player;");
		// log.debug("GETFIELD player");
		mv.visitVarInsn(ASTORE, 2);
		// log.debug("ASTORE 2");
		for (int i = 0; i < triggers.length; i++) {
			Label l = new Label();
			visitCondition(mv, triggers[i].getConditions(), l);
			visitAction(mv, triggers[i].getActions());
			mv.visitLabel(l);
			// log.debug("LABEL "+l);
		}
		mv.visitInsn(RETURN);
		// log.debug("RETURN");
		try {
			mv.visitMaxs(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mv.visitEnd();
	}

	protected void visitFinishCondition() {
		ExpressionList el = quest.getScript().getFinishCondition();
		visitConditionMethod("finishCondition", el);
	}

	protected void visitConsts() {
		ExpressionList el = quest.getPreCondition();
		if (el != null) {
			visitConsts(el);
		}
		el = quest.getScript().getFinishCondition();
		if (el != null) {
			visitConsts(el);
		}
		Trigger[] triggers = quest.getScript().getTriggers();
		for (int i = 0; i < triggers.length; i++) {
			visitConsts(triggers[i].getConditions());
			visitConsts(triggers[i].getActions());
		}
	}

	protected void visitConsts(ExpressionList el) {
		Expression[] exps = el.getExpressions();
		for (int i = 0; i < exps.length; i++) {
			visitConsts(exps[i]);
		}
	}

	protected void visitConsts(Expression exp) {
		Expression[] exps = exp.getExpressions();
		if (exps != null) { // 如果有子表达式说明不是Constant
			for (int i = 0; i < exps.length; i++) {
				visitConsts(exps[i]);
			}
		} else {
			if (exp instanceof Constant) {
				Value v = ((Constant) exp).getValue(null);
				if (v.getType() == Value.INT) { // 如果是int类型的常量,需要先查看有没有相同的常量已经生成
					int i = v.intValue();
					if (!intconst2name.containsKey(i)) {
						String fieldName = INT_CONST_PREFIX + (intconstcount++);
						intconst2name.put(i, fieldName);
						FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_FINAL
								+ ACC_STATIC, fieldName, "I", null,
								new Integer(i));
						fv.visitEnd();
					}
				} else {
					String s = v.stringValue();
					if (!stringconst2name.containsKey(s)) { // 如果是string类型的常量,需要先查看有没有相同的常量已经生成
						String fieldName = STRING_CONST_PREFIX
								+ (stringconstcount++);
						stringconst2name.put(s, fieldName);
						FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_FINAL
								+ ACC_STATIC, fieldName, "Ljava/lang/String;",
								null, s);
						fv.visitEnd();
					}
				}
			}
		}
	}
}
