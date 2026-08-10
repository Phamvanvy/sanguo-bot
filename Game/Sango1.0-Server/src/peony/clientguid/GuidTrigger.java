package peony.clientguid;

public class GuidTrigger {

	/** ID */
	public int id;
	/** 函数名称 */
	public String functionName;
	/** 参数类型(0为Integer、1为String) */
	public int[] paramType;
	/** 参数值 */
	public Object[] paramValue;
	/** 参数符号(0为小于,1为等于,2为大于) */
	public int[] paramSign;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFunctionName() {
		return functionName;
	}
	
	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}
	
	public int[] getParamType() {
		return paramType;
	}
	
	public void setParamType(int[] paramType) {
		this.paramType = paramType;
	}
	
	public Object[] getParamValue() {
		return paramValue;
	}
	
	public void setParamValue(Object[] paramValue) {
		this.paramValue = paramValue;
	}
	
	public int[] getParamSign() {
		return paramSign;
	}
	
	public void setParamSign(int[] paramSign) {
		this.paramSign = paramSign;
	}
	
}
