/**
 * Add all existing organizations to web page
 */
function getOrgs() {
  const qs = '/sql?' + updateQueryString('keyword', '');
  fetch(qs);
}

/**
 * Add the searched organizations by keyword
 */
function searchOrgs() {
  // remove previously displayed similar organizations
  var similarOrgs = document.getElementById('existing-organizations');
  while (similarOrgs.firstChild) {
    similarOrgs.removeChild(similarOrgs.firstChild);
  }

  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword);
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  })
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
  linkElement.setAttribute('href', org.link);
  linkElement.innerText = org.link;
  orgElement.appendChild(linkElement);
  const aboutElement = document.createElement('p');
  aboutElement.innerText = org.about;
  orgElement.appendChild(aboutElement);
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
