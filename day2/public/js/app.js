$(document).ready(function() {
    const fetchTunings = () => {
        $.get('/query-tunings', (data) => {  // Use relative URL
            const tableBody = $('#tuning-table-body');
            tableBody.empty();
            data.forEach((row, index) => {
                tableBody.append(`
                    <tr>
                        <td>${index + 1}</td>
                        <td>${row.query || ''}</td>
                        <td>${row.execution_time || ''}</td>
                        <td>${row.intervention || ''}</td>
                    </tr>
                `);
            });
        });
    };

    $('#tuning-form').submit(function(event) {
        event.preventDefault();
        const query = $('#query').val();
        const executionTime = $('#execution-time').val();
        const intervention = $('#intervention').val();

        // Log the form data to the console
        console.log('Form Data:', { query, executionTime, intervention });

        // Ensure AJAX POST sends JSON data
        $.ajax({
            url: '/query-tunings',  // Use relative URL
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ 
                query: query, 
                execution_time: parseFloat(executionTime), 
                intervention: intervention 
            }),
            success: function(response) {
                console.log('Server Response:', response);
                fetchTunings();
                $('#tuning-form')[0].reset();
            }
        });
    });

    fetchTunings();
});
