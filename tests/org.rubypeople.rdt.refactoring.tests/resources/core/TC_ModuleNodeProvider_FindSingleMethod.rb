module Modul
  def Modul.test_method
  end
end

module OuterModul
  module Modul
    def Modul.test_method
    end
  end
end

module Modul
  def Modul.test_method
  end
end