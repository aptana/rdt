class ServerConnectionTest < Test::Unit::TestCase
  
  def test_server_host_not_found
    assert_throws :host_not_found do
      @server = Server.new(:host => "misto.chh")
    end
  end
  
  def test_server_login_failed
    assert_throws :login_failed do
      @server = Server.new(:host => "misto.ch", :user => "rss", :pass => "a")
    end
  end  
  
    def test_server_creation
    assert_nothing_thrown do
      create_simple_server
      assert @server.connected
    end
  end
  
  def create_simple_server
    @server = Server.new :host => "misto.ch", :user => "rss", :pass => "a"
  end
  
end