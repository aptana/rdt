class X 
  def methode(param)
    if not param
      return "error"
    end
    puts "hello #{param}"
  end
  
  def meth
    x = X.new
    name = "mirko"
    var = x.methode name
  end 
end