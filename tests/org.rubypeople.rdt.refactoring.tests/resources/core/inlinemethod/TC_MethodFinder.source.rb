##!test5
class Level1
  def method_to_find; end
end
class MyClass < Level1; end

t = MyClass.new
t.method_to_find

##!test4
class Level1
  def method_to_find; end
end
class Level2 < Level1; end
class Level3 < Level2; end
class MyClass < Level3; end

t = MyClass.new
t.method_to_find

##!test3
class Test
  def test
    "test"
  end
  alias old_test test
  def test
    puts "calling new test:"
    old_test
  end
end

def test
  "test"
end

t = Test.new
t.test

##!test2
class Test
  def test
    "test"
  end
end

def test
  "test"
end


t = Test.new
t.test
##!test1
class Test
  def test
    "test"
  end
end

t = Test.new
t.test