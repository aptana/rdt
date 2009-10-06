class X 
  def say_hello to, i
    i.times { puts "hello #{to}!" }
  end
end

@x = X.new
name = "mirko"
@x.say_hello name, 5
