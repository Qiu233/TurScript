package vm;

public class ByteCode {
	public CmdType opcode;
	public int arg;
	public Object extra;

	public ByteCode(CmdType opcode, int arg, Object extra) {
		this.opcode = opcode;
		this.arg = arg;
		this.extra = extra;
	}
}
