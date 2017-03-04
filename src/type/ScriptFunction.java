package type;

import vm.ByteCode;

public class ScriptFunction extends Function{
	public ByteCode code[];

	public ScriptFunction(ByteCode code[]) {
		this.code = code;
	}

	public ScriptFunction(ByteCode code[], int len) {
		this.code = code;
		this.args_len = len;
	}
}
