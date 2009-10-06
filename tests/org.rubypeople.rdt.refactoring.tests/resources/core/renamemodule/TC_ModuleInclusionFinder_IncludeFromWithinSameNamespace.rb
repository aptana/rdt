module Modul
  module M
  end
  class Klass
    include M
  end
end

class Klasse
  include Modul::M
end
