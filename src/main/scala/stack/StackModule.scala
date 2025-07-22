package stack

import chisel3.stage.ChiselStage
import java.nio.file.Paths
import chisel3._
import chisel3.util._

// Your code starts here

class StackModule(val dataWidth: Int, val len: Int) extends Module {
  val io = IO(new Bundle {
    val in        = Input(UInt(32.W))             // Full instruction
    val out       = Output(UInt(dataWidth.W))     // Data output
    val underflow = Output(Bool())
    val overflow  = Output(Bool())
    val isEmpty   = Output(Bool())
    val isFull    = Output(Bool())
    val popped    = Output(Bool())
    val peeked    = Output(Bool())
  })

  // Stack and pointer
  val stack = Reg(Vec(len, UInt(dataWidth.W)))
  val sp    = RegInit(0.U(log2Ceil(len + 1).W))

  // Decode instruction
  val opcode = io.in(6, 0)
  val imm    = io.in(31, 7).asUInt()

  // Default outputs
  io.out       := 0.U
  io.underflow := false.B
  io.overflow  := false.B
  io.isEmpty   := (sp === 0.U)
  io.isFull    := (sp === len.U)
  io.popped    := false.B
  io.peeked    := false.B

  // Instruction execution
  switch(opcode) {
    is("b0100111".U) { // push
      when(io.isFull) {
        io.overflow := true.B
      } .otherwise {
        stack(sp) := imm(dataWidth - 1, 0)
        sp := sp + 1.U
      }
    }

    is("b1000011".U) { // pop
      when(io.isEmpty) {
        io.underflow := true.B
      } .otherwise {
        sp := sp - 1.U
        io.out := stack(sp - 1.U)
        io.popped := true.B
      }
    }

    is("b1000000".U) { // peek
      when(io.isEmpty) {
        io.underflow := true.B
      } .otherwise {
        io.out := stack(sp - 1.U)
        io.peeked := true.B
      }
    }
  }
}



// Your code ends here

object SVGen extends App {
  val out = Paths.get(
    "out",
    this.getClass
      .getName
      .stripSuffix("$")
  ).toString
  new ChiselStage().emitSystemVerilog(
    new StackModule(args(0).toInt, args(1).toInt),
    Array("--target-dir", out),
  )
}