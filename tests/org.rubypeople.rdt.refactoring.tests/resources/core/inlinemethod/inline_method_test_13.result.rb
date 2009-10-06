class A

  attr_accessor :x

  def a
    @x = 5
  end
end
class B
  def b
    a = A.new
    a.x = 5
  end
end
