class X 
  def say_hello to
    puts "hello #{to}!"
  end
  
  def mirko
    @x = X.new
    @x.say_hello "robin"
  end 
end
