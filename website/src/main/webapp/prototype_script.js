/**
 * Add the searched organizations by keyword
 */
function searchOrgs(page) {
  removeOrgs();
  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword) + '&' + updateQueryString('page', page);
  addTitle(keyword);
  addPagination();
  addOrgs(qs);
}

function addPagination() {
  document.getElementById('pagination').style.display = 'inline-block';
}

function addOrgs(qs) {
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  });
}

function removeOrgs() {
  // remove previously displayed similar organizations
  var existingOrgs = document.getElementById('existing-organizations');
  while (existingOrgs.firstChild) {
    existingOrgs.removeChild(existingOrgs.firstChild);
  }
}

/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}

/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('a');
  nameElement.setAttribute('href', 'https://' + org.link);
  nameElement.setAttribute('target', '_blank');
  nameElement.innerText = org.name;
  orgElement.appendChild(nameElement);
  const aboutElement = document.createElement('p');
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);
  const neighborElement = document.createElement('p');
  neighborElement.innerText = 'Like this: ' + org.neighbor1 + ', ' + org.neighbor2 + ', ' + org.neighbor3 + ', ' + org.neighbor4;
  orgElement.appendChild(neighborElement);
  return orgElement;
}

function addListener() {
  const inputBox = document.getElementById('keyword');
  inputBox.addEventListener('keyup', function(event) {
      if (event.key === 'Enter') {
        searchOrgs(0);
      }
  });
}

function addTitle(keyword) {
  const element = document.getElementById('results-title');
  element.innerText = 'Results for [' + keyword + ']: ';
}
