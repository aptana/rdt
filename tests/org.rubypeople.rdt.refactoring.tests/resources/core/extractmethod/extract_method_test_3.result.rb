class ReporterMainWindow < MainWindow
  def submit_reports
    service = ReporterReporterPort.new(@server_url)
    add_reports service
    @reports = []
    @entry_listview.clear
  end
  
  def add_reports service
    @reports.each do |report|
      service.addReport(Report.projects[report.project], @person_id, @apikey, report.start_t.to_string(Qt::ISODate), report.end_t.to_string(Qt::ISODate), report.desc)
    end
  end
  private :add_reports
  
end