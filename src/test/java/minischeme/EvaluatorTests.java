package minischeme;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static java.util.stream.Collectors.joining;

class EvaluatorTests {

  private Evaluator evaluator;
  private Map<String, Object> env;

  @BeforeEach void init() {
    this.evaluator = new Evaluator();
    this.env = GlobalEnvironment.build();
  }

  @Test void multiplicationTest() {
    List<Object> program = List.of("*", 2.0, 3.0, 4.0);
    Object result = evaluator.eval(program, env);
    assertEquals(24.0, result);
  }

  @Test void variableDefinitionTest() {
    List<Object> program = List.of("define", "pi", 3.141592);
    Object result = evaluator.eval(program, env);
    assertEquals(3.141592, env.get("pi"));
  }

  @Test void variableUseTest() {
    /*
    (begin
      (define pi 3.141592)
      (* pi 100.0))
    */
    List<Object> program = List.of(
        "begin",
        List.of("define", "pi", 3.141592),
        List.of("*", "pi", 100.0));
    Object result = evaluator.eval(program, env);
    assertEquals(314.1592, result);
  }

  @Test void lambdaExpressionTest() {
    /*
    (define aire-cercle (lambda (r) (* pi r r)))
    */
    List<Object> program = List.of("define", "aire-cercle", List.of("lambda", List.of("r"), List.of("*", "pi", "r", "r")));
    Object result = evaluator.eval(program, env);
    assertNotNull(env.get("aire-cercle"));
  }

  @Test void anonymousLambdaTest() {
    /*
    (begin
      (define pi 3.141592)
      ((lambda (r) (* pi r r)) 10.0)
    */
    List<Object> program = List.of(
        "begin",
          List.of("define", "pi", 3.141592),
          List.of(List.of("lambda", List.of("r"), List.of("*", "pi", "r", "r")), 10.0));
    Object result = evaluator.eval(program, env);
    assertEquals(314.1592, result);
  }

  @Test void lambdaDefinitionTest() {
    /*
    (begin
      (define pi 3.141592)
      (define aire-cercle (lambda (r) (* pi r r)))
      (aire-cercle 10.0))
    */
    List<Object> program = List.of(
        "begin",
          List.of("define", "pi", 3.141592),
          List.of("define", "aire-cercle", List.of("lambda", List.of("r"), List.of("*", "pi", "r", "r"))),
          List.of("aire-cercle", 10.0));
    Object result = evaluator.eval(program, env);
    assertEquals(314.1592, result);
  }

  @Test void lessThanTest() {
    assertTrue((boolean) evaluator.eval(List.of("<", 1.0, 2.0), env));
    assertTrue((boolean) evaluator.eval(List.of("<", 1.0, 2.0, 3.0), env));
    assertFalse((boolean) evaluator.eval(List.of("<", 2.0, 1.0), env));
    assertFalse((boolean) evaluator.eval(List.of("<", 2.0, 3.0, 1.0), env));
  }

  @Test void substractionTest() {
    assertEquals(2.0, evaluator.eval(List.of("-", 3.0, 1.0), env));
    assertEquals(2.0, evaluator.eval(List.of("-", 5.0, 2.0, 1.0), env));
  }

  @Test void ifTest() {
    List<Object> program = List.of("if", List.of("<", 1.0, 2.0), List.of("*", 2.0, 2.0), List.of("*", 3.0, 3.0));
    Object result = evaluator.eval(program, env);
    assertEquals(4.0, result);
  }

  @Test void elseTest() {
    List<Object> program = List.of("if", List.of("<", 2.0, 1.0), List.of("*", 2.0, 2.0), List.of("*", 3.0, 3.0));
    Object result = evaluator.eval(program, env);
    assertEquals(9.0, result);
  }

  @Test void recursionTest() {
    /*
    (begin
      (define facto (lambda (n)
        (if (< n 2.0)
          1.0
          (* n (facto (- n 1.0))))))
      (facto 5.0))
    */
    List<Object> program =
      List.of("begin",
        List.of("define", "facto", List.of("lambda", List.of("n"),
          List.of("if", List.of("<", "n", 2.0),
            1.0,
            List.of("*", "n", List.of("facto", List.of("-", "n", 1.0)))))),
        List.of("facto", 5.0));
    Object result = evaluator.eval(program, env);
    assertEquals(120.0, result);
  }

  @Test void notTest() {
    assertTrue((boolean) evaluator.eval(List.of("not", false), env));
    assertFalse((boolean) evaluator.eval(List.of("not", true), env));
  }

  @Test void andTest() {
    assertTrue((boolean) evaluator.eval(List.of("and", true, true), env));
    assertFalse((boolean) evaluator.eval(List.of("and", true, true, false), env));
    Object param1 = evaluator.eval(List.of("not", false), env);
    Object param2 = evaluator.eval(List.of("<", 1.0, 2.0), env);
    assertTrue((boolean) evaluator.eval(List.of("and", true, param1, param2), env));
  }

  @Test void eqTest() {
    assertTrue((boolean) evaluator.eval(List.of("eq", 1.001, 1.002, 1.003, 1.004), env));
    assertFalse((boolean) evaluator.eval(List.of("eq", 1.001, 1.002, 1.003, 1.005), env));
    assertFalse((boolean) evaluator.eval(List.of("eq", 1.00, 1.01, 1.02, 1.03), env));
  }

  @Test void countTest() {
    /*
    (begin
      (count (list 1.0 3.0 5.0 7.0))
    */
    List<Object> program = List.of(
            "begin",
            List.of("count", List.of("list", 1.0, 3.0, 5.0, 7.0)));
    Object result = evaluator.eval(program, env);
    assertEquals(4.0, result);
  }

  @Test void headTest() {
    /*
    (begin
      (head (list 7.0 4.0 1.0 5.0))
    */
    List<Object> program = List.of(
            "begin",
            List.of("head", List.of("list", 7.0, 4.0, 1.0, 5.0)));
    Object result = evaluator.eval(program, env);
    assertEquals(7.0, result);
  }

  @Test void tailTest() {
    /*
    (begin
      (head (list 1.0 2.0 3.0 4.0))
    */
    List<Object> program = List.of(
            "begin",
            List.of("tail", List.of("list", 1.0, 2.0, 3.0, 4.0)));
    Object result = evaluator.eval(program, env);
    assertEquals( "[2.0, 3.0, 4.0]", result.toString());
  }
}
