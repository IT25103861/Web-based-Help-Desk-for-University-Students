import os
import re

src_dir = r'src/main/java/com/campus/helpdesk'

replacements = [
    (r'\.getStudentId\(\)', r'.getStudent().getUserId()'),
    (r'\.getFacultyId\(\)', r'.getFaculty().getFacultyId()'),
    (r'\.getCategoryId\(\)', r'.getCategory().getCategoryId()'),
    (r'\.getAssignedStaffId\(\)', r'.getAssignedStaff().getUserId()'),
    
    # Repositories findBy methods were already updated manually
    (r'findByStudentIdOrderBy', r'findByStudent_UserIdOrderBy'),
    (r'findByFacultyIdOrderBy', r'findByFaculty_FacultyIdOrderBy'),
    (r'existsByCategoryId', r'existsByCategory_CategoryId'),
    
    # Setters - creating proxy objects
    (r'\.setStudentId\(([^)]+)\)', r'.setStudent(new com.campus.helpdesk.entity.User() {{ setUserId(\1); }})'),
    (r'\.setFacultyId\(([^)]+)\)', r'.setFaculty(new com.campus.helpdesk.entity.Faculty() {{ setFacultyId(\1); }})'),
    (r'\.setCategoryId\(([^)]+)\)', r'.setCategory(new com.campus.helpdesk.entity.Category() {{ setCategoryId(\1); }})'),
    (r'\.setAssignedStaffId\(([^)]+)\)', r'.setAssignedStaff(new com.campus.helpdesk.entity.User() {{ setUserId(\1); }})'),
    (r'\.setTicketId\(([^)]+)\)', r'.setTicket(new com.campus.helpdesk.entity.Ticket() {{ setTicketId(\1); }})'),
    (r'\.setUserId\(([^)]+)\)', r'.setUser(new com.campus.helpdesk.entity.User() {{ setUserId(\1); }})')
]

for root, _, files in os.walk(src_dir):
    for file in files:
        if file.endswith('.java') and not root.endswith('entity'):
            path = os.path.join(root, file)
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            new_content = content
            for old, new in replacements:
                new_content = re.sub(old, new, new_content)
                
            if new_content != content:
                with open(path, 'w', encoding='utf-8') as f:
                    f.write(new_content)
                print(f'Updated {path}')
