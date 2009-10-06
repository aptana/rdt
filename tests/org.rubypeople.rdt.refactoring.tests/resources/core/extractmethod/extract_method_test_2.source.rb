class HttpResponse
  def send_file(path, small_file = false)
    if small_file
      File.open(path, "rb") {|f| @socket << f.read }
    else
      File.open(path, "rb") do |f|
        while chunk = f.read(Const::CHUNK_SIZE) and chunk.length > 0
          begin
            write(chunk)
          rescue Object => exc
            break
          end
        end
      end
    end
    @body_sent = true
  end
end