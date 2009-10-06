class A
  def a
    @x = 5
  end
end
class B
  def b
    a = A.new
    a.a
  end
end
