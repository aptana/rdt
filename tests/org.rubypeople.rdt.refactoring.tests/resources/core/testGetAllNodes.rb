class Test
	attr_reader :var
	def method
		@var = 5
	end
end

Test.new.method