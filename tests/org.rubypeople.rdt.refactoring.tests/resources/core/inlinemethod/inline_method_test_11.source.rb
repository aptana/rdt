class MathBase
  def twenty_two
    22
  end
end

class Math < MathBase
  def get_pi
    twenty_two / seven
  end
  def seven
    7
  end
end

math = Math.new
pi = math.get_pi
