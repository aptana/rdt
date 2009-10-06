class MathBase
  def twenty_two
    22
  end
end

class Math < MathBase
  def seven
    7
  end
end

math = Math.new
pi = (math.twenty_two / math.seven)
