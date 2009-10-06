require 'includedFile'

class TestKlasse

  def initialize
    @field = ATargetClass.new
  end
  
  #comment before writer
  attr_writer :var #comment after writer
  #comment before reader
  attr_reader :var #comment after reader
  
  def five_plus arg
   5 + arg
  end
 
  def calculate
    #comment before assignment
    @var = five_plus 1 #comment after assignment
  end
  
  def use_var
  	#before use var
    @var += "." #after use var
  end
  
end

t = TestKlasse.new
t.calculate
#comment before usage
puts t.var #comment after usage