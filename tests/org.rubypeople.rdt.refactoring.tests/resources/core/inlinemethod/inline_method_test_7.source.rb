module Module
  class Klass
    def method
      return "return"
    end
  end
end

k = Module::Klass.new
result = k.method