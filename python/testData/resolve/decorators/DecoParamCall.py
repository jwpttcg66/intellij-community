def deco(prefix):
  def fun(f):
    print f
    def dfun():
      return [prefix, f()]
    return dfun
  return fun

@<ref>deco(1)
def foo():
  pass

# same as in callee test
