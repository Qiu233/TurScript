package vm;

public enum CmdType {
	NOP, // 空指令
	HALT, // 关闭虚拟机
	POP, // 弹出栈顶元素
	POPN, // 弹出栈顶N个元素
	OPTR, // 执行计算
	LOAD_CONST, // 加载常量
	LOAD_NULL, // 加载NULL
	NEW_VAR, // 创建变量
	SET_VAR, // 赋值变量
	LOAD_VAR, // 加载变量
	CALL, // 调用函数
	TEST, // 真值跳转
	JMP, // 无条件跳转
	JMP_BACK, // 向后无条件跳转
	LOAD_TRUE, // 加载TRUE
	LOAD_FALSE, // 加载FALSE
	SET_RET_VAL, // 设置返回值
	RET, // 函数返回
	LOAD_LOCAL, // 加载局部变量
	NEW_LOCAL, // 在栈上新建局部变量，函数结束后栈内有关该函数的元素全部会被弹出
	SET_LOCAL, // 赋值局部变量
}
