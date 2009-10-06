class X 
  def say_hello to
    puts "hello #{to}!"
  end
  
  def mirko
    @x = X.new
    name = "mirko"
    @x.say_hello name
  end 
end
