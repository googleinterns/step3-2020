/**
 * Add all existing organizations to web page
 */
function getOrgs() {
  removeOrgs()
  const qs = '/sql?' + updateQueryString('keyword', '');
  addOrgs(qs);
}

/**
 * Add the searched organizations by keyword
 */
function searchOrgs() {
  removeOrgs()
  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword);
  addOrgs(qs);
}

function addOrgs(qs) {
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  })
}

function removeOrgs() {
  // remove previously displayed similar organizations
  var existingOrgs = document.getElementById('existing-organizations');
  while (existingOrgs.firstChild) {
    existingOrgs.removeChild(existingOrgs.firstChild);
  }
}

/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('li');
  const nameElement = document.createElement('h4');
  nameElement.innerText = org.id + '. ' + org.name;
  orgElement.appendChild(nameElement);
  const linkElement = document.createElement('a');
  linkElement.setAttribute('href', 'https://' + org.link);
  linkElement.setAttribute('target', '_blank');
  linkElement.innerText = org.link;
  orgElement.appendChild(linkElement);
  const aboutElement = document.createElement('p');
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);
  const neighborElement = document.createElement('p');
  neighborElement.innerText = org.neighbor1 + ', ' + org.neighbor2 + ', ' + org.neighbor3 + ', ' + org.neighbor4;
  orgElement.appendChild(neighborElement);
  return orgElement;
}

/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}
