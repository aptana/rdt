class ATargetClass
  
  attr_accessor :var
  
  def test
    "test"
  end
end

class TestKlasse

  def initialize
    @field = ATargetClass.new
  end
  
  def calculate
    @field.var = 1
  end  
end