class X 
  def methode(param)
    puts "hello #{param}"
  end
  
  def meth
    x = X.new
    name = "mirko"
    var = x.methode name
  end 
end