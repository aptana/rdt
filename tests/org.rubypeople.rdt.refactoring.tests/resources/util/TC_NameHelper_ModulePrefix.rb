module M1
  var = 5
end

var = 5

module M1
  module M3
    M1.test
  end
end


module OuterModule
  module Module
  end
end

module OuterModule
  class Test
    include Module
  end
end