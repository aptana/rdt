outside = "asdf"

class X
  attr_accessor :x
  def m
    puts @x
    i = 1
  end
  public :m
end

def outside_m
  @stupid_field = 5
  i = 3
end

"asdf"