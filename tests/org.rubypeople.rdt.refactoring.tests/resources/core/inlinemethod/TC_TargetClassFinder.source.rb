##!test14
k = Module1::Module2::Module3::Klass.new
result = k.method
##!test13
k = Module::Klass.new
result = k.method
##!test12
var = Test.new
var.call
##!test11
class Employee
  def work!
    work("a")
  end
end

@e = Employee.new
@e.work!
##!test10
class TestClass
  def initialize
    var = SomeClass.new
    p var.class
  end
end
##!test9
class TestClass
  def initialize
    @var.to_lower
  end
end
##!test8
class TestClass
  def initialize
    method
  end
  def method
  end
end
##!test7
class TestClass
  def initialize
    @var = TestClass.new
  end
end
class TestClass2
  def initialize
    @var = {}
  end
end
class TestClass
  def method
    puts @var.inspect
  end
end
##!test6
class TestClass
  def initialize
    @var = [0]
  end
end
class TestClass2
  def initialize
    @var = {}
  end
end
class TestClass
  def method
    puts @var.inspect
  end
end
##!test5
class TestClass
  def initialize
    @var = [0]
  end
  def method
    puts @var.inspect
  end
end
##!test4
class TestClass
  def method
    @var = 5
    puts @var.inspect
  end
end
##!test3
class TestClass
  def method
    var = []
  	[].each do |i|
  	  var << i
  	  puts var.to_upper
  	end
  end
end
##!test2
def methode
  var = []
  p var
  var = {}
  puts var.inspect
  var = ""
  puts var.inspect
end
##!test1
var = String.new
puts var.to_lower