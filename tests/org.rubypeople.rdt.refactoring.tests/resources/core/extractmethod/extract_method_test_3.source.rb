class ReporterMainWindow < MainWindow
  def submit_reports
    service = ReporterReporterPort.new(@server_url)
    @reports.each do |report|
      service.addReport(Report.projects[report.project], @person_id, @apikey, report.start_t.to_string(Qt::ISODate), report.end_t.to_string(Qt::ISODate), report.desc)
    end
    @reports = []
    @entry_listview.clear
  end
end