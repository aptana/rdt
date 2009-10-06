module Module
  class Klass
    def method
      return "return"
    end
  end
end

k = Module::Klass.new
eins, result = 1, k.method