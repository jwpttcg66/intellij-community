package com.jetbrains.python;

import com.intellij.openapi.application.PathManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.ResolveTestCase;
import com.jetbrains.python.psi.PyArgumentList;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyExpression;

/**
 * Decorator-specific tests.
 * User: dcheryasov
 * Date: Dec 28, 2008 3:50:23 AM
 */
public class PyDecoratorTest extends ResolveTestCase {
  private PsiElement find() throws Exception {
    PsiReference ref = configureByFile(getTestName(false) + ".py");
    return ref.getElement();
  }

  public void testDecoCall() throws Exception {
    PsiElement targetElement = find().getParent();
    assertTrue(targetElement instanceof PyDecorator);
    PyDecorator deco = (PyDecorator)targetElement;
    PyFunction decofun = deco.getTarget();
    assertNotNull(decofun);
    assertEquals("foo", decofun.getName());
    assertFalse(deco.isBuiltin());
    assertFalse(deco.hasArgumentList());
  }

  public void testDecoParamCall() throws Exception {
    PsiElement targetElement = find().getParent();
    assertTrue(targetElement instanceof PyDecorator);
    PyDecorator deco = (PyDecorator)targetElement;
    PyFunction decofun = deco.getTarget();
    assertNotNull(decofun);
    assertEquals("foo", decofun.getName());
    assertFalse(deco.isBuiltin());
    assertTrue(deco.hasArgumentList());
    PyArgumentList arglist = deco.getArgumentList();
    assertNotNull(arglist);
    PyExpression[] args = arglist.getArguments();
    assertEquals("argument count", 1, args.length);
    assertEquals("argument value", "1", args[0].getText());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath() + "/plugins/python/testData/resolve/decorators/";
  }
}
