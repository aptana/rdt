class Test
  def methode
    one, two, three, four, five = 1..5
    
    new_method(two, one)
    puts three
    puts four
    puts five
  end
  
  def new_method two2, one1
    puts one1
    puts two2
  end
  private :new_method
  
end