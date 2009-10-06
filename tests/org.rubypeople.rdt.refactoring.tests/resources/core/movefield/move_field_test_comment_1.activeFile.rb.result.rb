require 'includedFile'

class TestKlasse
  
  def initialize
    @field = ATargetClass.new
  end
  
  
  def five_plus arg
    5 + arg
  end
  
  def calculate
  	#comment before assignment
    @field.var = five_plus 1 #comment after assignment
  end
  
  def use_var
  	#before use var
    @field.var += "." #after use var
  end
  
  #comment before reader
  #comment after reader
  def var
    @field.var
  end
  public :var
  
  #comment before writer
  #comment after writer
  def var= var
    @field.var = var
  end
  public :var=
  
end

t = TestKlasse.new
t.calculate
#comment before usage
puts t.var #comment after usage