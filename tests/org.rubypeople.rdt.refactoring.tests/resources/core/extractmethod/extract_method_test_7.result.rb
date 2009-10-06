class Test
  def test
    string, hash, array = init
    do_something(string, hash, array)
  end
  
  def init
    string = ""
    hash = {}
    array = []
    [string, hash, array]
  end
  
end
