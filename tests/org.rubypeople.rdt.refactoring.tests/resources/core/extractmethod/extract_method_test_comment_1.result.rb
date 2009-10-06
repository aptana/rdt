class Test
  def methode
    one, two, three, four, five = 1..5
    
    extr(one, two)
    puts three
    puts four
    puts five
  end
  
  def extr one, two
    #before one
    puts one #after one
    #before two
    puts two #after two
  end
  private :extr
  
end