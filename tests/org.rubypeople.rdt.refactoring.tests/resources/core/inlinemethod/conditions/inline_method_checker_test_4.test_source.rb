class X 
  def methode(param)
    puts "hello #{param}"
    if(param == 1)
      return true
    else 
      return false
    end
  end
  
  def meth
    x = X.new
    name = "mirko"
    var = x.methode name
  end 
end
