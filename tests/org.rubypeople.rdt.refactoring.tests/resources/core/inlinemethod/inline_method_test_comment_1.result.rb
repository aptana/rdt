class X 
  def say_hello to
    #before hello
    puts "hello #{to}!" #after hello
  end
  
  def mirko
    @x = X.new
    name = "mirko"
    #before hello
    puts "hello #{name}!" #after hello
  end 
end
