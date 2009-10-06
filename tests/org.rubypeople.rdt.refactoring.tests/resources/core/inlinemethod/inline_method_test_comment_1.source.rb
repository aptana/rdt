class X 
  def say_hello to
  	#before hello
    puts "hello #{to}!" #after hello
  end
  
  def mirko
    @x = X.new
    name = "mirko"
    @x.say_hello name
  end 
end
