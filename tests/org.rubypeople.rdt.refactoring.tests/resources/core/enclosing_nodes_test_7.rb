  def initialize(comm, uploader)
    super(nil, "Blog")
    @communicator = comm
    @entries = []
    @entry_in_process = nil
    @categories = []
    @uploader = uploader
  end