package vm;

public class Context {

	public ByteCode code[];
	public int ip = 0;
	public boolean halt = false;
	public boolean isFunc;
	public int stackSize = 0;
	public int stackBase = 0;

	public Context(ByteCode code[], boolean isFunc, int stackSize, int stackBase) {
		this.code = code;
		this.isFunc = isFunc;
		this.stackSize = stackSize;
		this.stackBase = stackBase;
	}
}
