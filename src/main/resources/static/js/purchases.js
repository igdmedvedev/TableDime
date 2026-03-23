let allCategories = [];

// Загрузка категорий один раз при старте
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('/purchases/categories');
        allCategories = await response.json();
    } catch (e) { console.error("Ошибка загрузки категорий", e); }
});

function checkRowChanges(input) {
    const row = input.closest('tr');
    if (!row) return;

    const fields = row.querySelectorAll('.row-field');
    let hasChanges = false;

    fields.forEach(f => {
        const currentVal = f.value.trim();
        const originalVal = f.getAttribute('data-original') || "";

        if (f.type === 'number') {
            if (parseFloat(currentVal) !== parseFloat(originalVal)) {
                if (!(isNaN(parseFloat(currentVal)) && isNaN(parseFloat(originalVal)))) {
                    hasChanges = true;
                }
            }
        } else {
            if (currentVal !== originalVal.trim()) {
                hasChanges = true;
            }
        }
    });

    const id = row.getAttribute('data-id');
    const applyBtn = document.getElementById(`apply-${id}`);
    const resetBtn = document.getElementById(`reset-${id}`);

    if (applyBtn && resetBtn) {
        if (hasChanges) {
            applyBtn.classList.remove('d-none');
            resetBtn.classList.remove('d-none');
        } else {
            applyBtn.classList.add('d-none');
            resetBtn.classList.add('d-none');
        }
    }
}

function resetRow(id) {
    const row = document.querySelector(`tr[data-id="${id}"]`);
    if (!row) return;

    clearErrors(row);

    row.querySelectorAll('.row-field').forEach(f => {
        f.value = f.getAttribute('data-original');
    });

    const applyBtn = document.getElementById(`apply-${id}`);
    const resetBtn = document.getElementById(`reset-${id}`);
    applyBtn.classList.add('d-none');
    resetBtn.classList.add('d-none');
}

async function saveRow(id) {
    const row = document.querySelector(`tr[data-id="${id}"]`);
    clearErrors(row);

    const fields = row.querySelectorAll('.row-field');
    const data = {
        amt: fields[0].value,
        date: fields[1].value,
        category: fields[2].value,
        comments: fields[3].value
    };

    try {
        const res = await fetch(`/purchases/${id}`, {
            method: 'PUT',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        });

        if (res.ok) {
            window.location.reload();
        } else if (res.status === 400) {
            const errors = await res.json();
            showErrors(row, errors);
        } else {
            alert("Произошла системная ошибка");
        }
    } catch (e) {
        console.error(e);
    }
}

// ЛОГИКА МОДАЛЬНОГО ОКНА (BATCH ADD)
function addBatchRow() {
    const tbody = document.getElementById('batchTableBody');
    const rows = tbody.querySelectorAll('tr');
    let lastDate = "";

    if (rows.length > 0) {
        lastDate = rows[rows.length - 1].querySelector('.b-date').value;
    }

    const tr = document.createElement('tr');
    const rowId = Date.now();
    tr.innerHTML = `
        <td><input type="number" step="0.01" class="form-control form-control-sm b-amt" required></td>
        <td><input type="date" class="form-control form-control-sm b-date" value="${lastDate}" required></td>
        <td class="position-relative">
            <input type="text" class="form-control form-control-sm b-cat" autocomplete="off" 
                   oninput="handleCategoryInput(this)" onfocus="handleCategoryInput(this)">
            <ul class="category-dropdown"></ul>
        </td>
        <td><input type="text" class="form-control form-control-sm b-comm"></td>
        <td><button class="btn btn-sm text-danger" onclick="this.closest('tr').remove()">✕</button></td>
    `;
    tbody.appendChild(tr);
}

function handleCategoryInput(input) {
    if (input.classList.contains('row-field')) {
        checkRowChanges(input);
    }

    const query = input.value.toLowerCase();
    const dropdown = input.nextElementSibling;
    const filtered = allCategories.filter(c => c.toLowerCase().includes(query));

    if (filtered.length > 0) {
        dropdown.innerHTML = filtered.map(c => `<li>${c}</li>`).join('');

        const rect = input.getBoundingClientRect();

        dropdown.style.display = 'block';
        dropdown.style.top = (rect.bottom + window.scrollY) + 'px';
        dropdown.style.left = (rect.left + window.scrollX) + 'px';
        dropdown.style.width = rect.width + 'px';

        dropdown.querySelectorAll('li').forEach(li => {
            li.onclick = () => {
                input.value = li.innerText;
                dropdown.style.display = 'none';

                if (input.classList.contains('row-field')) {
                    checkRowChanges(input);
                }
            };
        });
    } else {
        dropdown.style.display = 'none';
    }
}

document.querySelector('.custom-modal-body').addEventListener('scroll', () => {
    document.querySelectorAll('.category-dropdown').forEach(d => d.style.display = 'none');
});

document.addEventListener('click', (e) => {
    if (!e.target.classList.contains('b-cat')) {
        document.querySelectorAll('.category-dropdown').forEach(d => {
            d.style.display = 'none';
        });
    }
});

async function submitBatch() {
    const modalBody = document.querySelector('.custom-modal-body');
    clearErrors(modalBody);

    const rows = document.querySelectorAll('#batchTableBody tr');
    const data = Array.from(rows).map(r => ({
        amt: r.querySelector('.b-amt').value,
        date: r.querySelector('.b-date').value,
        category: r.querySelector('.b-cat').value,
        comments: r.querySelector('.b-comm').value
    }));

    const res = await fetch('/purchases/batch', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });

    if (res.ok) {
        window.location.reload();
    } else if (res.status === 400) {
        const errors = await res.json();
        rows.forEach(row => showErrors(row, errors));
    } else {
        alert("Ошибка при сохранении пачки данных");
    }
}

async function deleteRow(id) {
    if (!confirm('Удалить запись?')) return;

    const res = await fetch(`/purchases/${id}`, {
        method: 'DELETE'
    });

    if (res.ok) {
        window.location.reload(); // Или удалите строку из DOM через row.remove()
    } else {
        alert("Ошибка при удалении");
    }
}

// Открытие первой строки при открытии модалки
document.getElementById('batchAddModal').addEventListener('shown.bs.modal', () => {
    const tbody = document.getElementById('batchTableBody');
    if (tbody.children.length === 0) addBatchRow();
});

function clearErrors(container) {
    container.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
    container.querySelectorAll('.error-feedback').forEach(el => el.remove());
}

function showErrors(row, errors) {
    const fieldMap = {
        'amt': '.b-amt, input[type="number"]',
        'date': '.b-date, input[type="date"]',
        'category': '.b-cat'
    };

    for (const [field, message] of Object.entries(errors)) {
        const input = row.querySelector(fieldMap[field]);
        if (input) {
            input.classList.add('is-invalid');
            const errorDiv = document.createElement('div');
            errorDiv.className = 'error-feedback';
            errorDiv.innerText = message;
            input.parentNode.appendChild(errorDiv);
        }
    }
}