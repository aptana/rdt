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
  def Modul.second_method
  end
  def Modul.third_method
  end
end