class X 
  def say_hello to, i
    i.times { puts "hello #{to}!" }
  end
end

@x = X.new
name = "mirko"
i = 5
i.times { puts "hello #{name}!" }
